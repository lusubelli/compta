import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {AuthService} from "../../auth/auth.service";
import {CookieService} from "ngx-cookie-service";

@Component({
  selector: 'signin',
  templateUrl: './signin.component.html',
  styleUrls: ['./signin.component.scss']
})
export class SigninComponent implements OnInit {

  signinForm: FormGroup;
  submitted = false;

  constructor(private formBuilder: FormBuilder,
              private authService: AuthService,
              private cookieService: CookieService,
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
    this.authService.signin(JSON.stringify(this.signinForm.value, null, 4))
      .subscribe(response => {
        localStorage.setItem('token', this.cookieService.get('SESSIONID'));
        this.router.navigate(['account']);
      });
  }

}
