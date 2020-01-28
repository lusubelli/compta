package fr.usubelli.compta;

import fr.usubelli.compta.domain.nlp.*;
import opennlp.tools.ml.maxent.GISModel;
import opennlp.tools.ml.maxent.GISTrainer;
import opennlp.tools.ml.model.*;
import opennlp.tools.namefind.BilouCodec;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.ObjectStreamUtils;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class NlpEvaluation {

    private final GISModel intentModel;
    private final NameFinderME nameModel;


    NlpEvaluation(List<QualifiedSentence> sentences, Locale locale) throws IOException {
        this.intentModel = buildIntentModel(sentences);
        this.nameModel = buildNameModel(sentences, locale.toString());
    }

    Evaluation evaluate(String text) {
        return new Evaluation(evaluateIntent(text), evaluateNames(text));
    }

    private NameFinderME buildNameModel(List<QualifiedSentence> sentences, String language) throws IOException {
        List<NameSample> trainingEvents = new ArrayList<>();
        if (sentences.size() >= 2) {
            for (QualifiedSentence sentence : sentences) {
                trainingEvents.add(new NameSample(
                        SimpleTokenizer.INSTANCE.tokenize(sentence.getText()),
                        buildSpans(sentence.getText(), sentence.getQualifiedNames()),
                        false));
            }
        }

        if (trainingEvents.size() < 5) {
            return null;
        }

        return new NameFinderME(
                NameFinderME.train(
                        language,
                        null,
                        ObjectStreamUtils.createObjectStream(trainingEvents),
                        new TrainingParameters(),
                        new TokenNameFinderFactory(null, null, new BilouCodec())));
    }

    private Span[] buildSpans(String text, List<QualifiedName> qualifiedNames) {
        final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        final String[] tokenize = tokenizer.tokenize(text);
        final List<Span> spans = new ArrayList<>();
        for (QualifiedName qualifiedName : qualifiedNames) {
            int start;
            if (qualifiedName.getStart() == 0) {
                start = 0;
            } else {
                start = tokenizer.tokenize(text.substring(0, qualifiedName.getStart())).length;
            }
            int end = start + tokenizer.tokenize(text.substring(qualifiedName.getStart(), qualifiedName.getEnd())).length;
            if (start < tokenize.length && end <= tokenize.length) {
                spans.add(new Span(start, end, qualifiedName.getRole()));
            }
        }
        return spans.toArray(new Span[0]);
    }

    private GISModel buildIntentModel(List<QualifiedSentence> sentences) throws IOException {
        GISModel gisModel;
        if (sentences.size() < 2) {
            gisModel = new GISModel(new Context[0], new String[0], new String[0]);
        } else {
            final List<Event> events = new ArrayList<>();
            for (QualifiedSentence sentence : sentences) {
                events.add(new Event(sentence.getIntent(), SimpleTokenizer.INSTANCE.tokenize(sentence.getText())));
            }
            final TrainingParameters trainingParameters = new TrainingParameters();
            if (sentences.size() < 1000) {
                trainingParameters.put(TrainingParameters.CUTOFF_PARAM, 1);
            }
            final DataIndexer dataIndexer = buildDataIndexer(sentences);
            dataIndexer.init(trainingParameters, null);
            dataIndexer.index(ObjectStreamUtils.createObjectStream(events));
            gisModel = new GISTrainer().trainModel(1000, dataIndexer);
        }
        return gisModel;
    }

    private DataIndexer buildDataIndexer(List<?> sentences) {
        DataIndexer dataIndexer = new OnePassRealValueDataIndexer();
        if (sentences.size() >= 100) {
            dataIndexer = new TwoPassDataIndexer();
        }
        return dataIndexer;
    }

    private List<NameEvaluation> evaluateNames(String text) {
        final List<NameEvaluation> nameEvaluations = new ArrayList<>();
        if (nameModel != null) {
            final String[] tokens = SimpleTokenizer.INSTANCE.tokenize(text);
            final Span[] spans = nameModel.find(tokens);
            for (Span span : spans) {
                nameEvaluations.add(new NameEvaluation(
                        span.getType(),
                        start(tokens, span),
                        end(tokens, span),
                        span.getProb()
                ));
            }
        }
        return nameEvaluations;
    }

    private int start(String[] tokens, Span span) {
        int start = 0;
        for (int i = 0; i < span.getStart(); i++) {
            start += tokens[i].length();
            start++;
        }
        return start;
    }

    private int end(String[] tokens, Span span) {
        int end = 0;
        for (int i = 0; i < span.getEnd(); i++) {
            end += tokens[i].length();
            end++;
        }
        return end -1;
    }

    private List<IntentEvaluation> evaluateIntent(String text) {
        final double[] prob = intentModel.eval(SimpleTokenizer.INSTANCE.tokenize(text));
        final List<IntentEvaluation> intentEvaluations = new ArrayList<>();
        for (int i = 0; i < prob.length; i++) {
            intentEvaluations.add(new IntentEvaluation(intentModel.getOutcome(i), prob[i]));
        }
        return intentEvaluations;
    }

}
