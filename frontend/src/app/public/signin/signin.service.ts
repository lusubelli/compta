import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface Config {
  heroesUrl: string;
}

@Injectable()
export class SigninService {


  constructor(private http: HttpClient) { }

  signin(login: any) {
    return this.http.post('http://localhost:8080/signin', login);
  }

}
