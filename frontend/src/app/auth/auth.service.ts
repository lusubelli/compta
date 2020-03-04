import {Injectable} from '@angular/core';
import {JwtHelperService} from '@auth0/angular-jwt';
import {HttpClient} from "@angular/common/http";

@Injectable()
export class AuthService {

  constructor(public jwtHelper: JwtHelperService, private http: HttpClient) {
  }

  signin(login: any) {
    return this.http.post('http://localhost:8080/backend/signin', login/*, { withCredentials: true }*/);
  }

  isAuthenticated(): boolean {
    const token = localStorage.getItem('token');
    return !this.jwtHelper.isTokenExpired(token);
  }

}
