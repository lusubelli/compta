import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {SigninService} from "./signin.service";
import {Router} from "@angular/router";

@Component({
  selector: 'signin',
  templateUrl: './signin.component.html',
  styleUrls: ['./signin.component.scss']
})
export class SigninComponent implements OnInit {

  signinForm: FormGroup;
  submitted = false;

  constructor(private formBuilder: FormBuilder,
              private signinService: SigninService,
              private router: Router) { }

  ngOnInit(): void {
    this.signinForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(3)]]
    });
  }

  signin() {
    this.submitted = true;
    if (this.signinForm.invalid) {
      return;
    }
    this.signinService.signin(JSON.stringify(this.signinForm.value, null, 4))
      .subscribe(response => {
        console.log('SUCCESS!! :-)');
        localStorage.setItem('token', "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        this.router.navigate(['account']);
      });
  }

}
