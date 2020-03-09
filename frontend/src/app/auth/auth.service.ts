import {Injectable} from '@angular/core';
import {JwtHelperService} from '@auth0/angular-jwt';
import {HttpClient, HttpHeaders} from "@angular/common/http";

interface AccessToken {
  access_token: string;
}

@Injectable()
export class AuthService {

  constructor(public jwtHelper: JwtHelperService, private http: HttpClient) {
  }

  signin(login: any) {
    return this.http.post<AccessToken>('http://localhost:8080/backend/signin', login/*, { withCredentials: true }*/);
  }

  isAuthenticated(): boolean {
    const token = localStorage.getItem('xsrfToken');
    return !this.jwtHelper.isTokenExpired(token);
  }

  protected() {
    const token = localStorage.getItem('xsrfToken');
    console.log(token);
    const headers = new HttpHeaders({'x-xsrf-token':token});
    return this.http.get('http://localhost:8080/backend/protected', { headers, withCredentials: true });
  }

}
