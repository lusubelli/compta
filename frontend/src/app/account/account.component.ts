import {Component, OnInit} from '@angular/core';
import {AuthService} from "../auth/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.scss']
})
export class AccountComponent implements OnInit {

  constructor(private authService: AuthService,
              private router: Router) { }

  ngOnInit(): void {
    this.authService.account()
      .subscribe(response => {
        console.log("Hello");
      });
  }

  signout() {
    this.authService.signout()
      .subscribe(response => {
        localStorage.removeItem('xsrfToken');
        this.router.navigate(['signin']);
      });
  }

}
