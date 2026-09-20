import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, filter, switchMap, take, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  private refreshEmAndamento = false;
  private accessTokenRenovado$ = new BehaviorSubject<string | null>(null);

  constructor(private authService: AuthService) { }

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const authReq = this.comToken(req);

    return next.handle(authReq).pipe(
      catchError(erro => {
        if (erro instanceof HttpErrorResponse && erro.status === 401 && !this.ehRotaDeAuth(req)) {
          return this.tratarNaoAutorizado(req, next);
        }
        return throwError(() => erro);
      })
    );
  }

  private comToken(req: HttpRequest<unknown>): HttpRequest<unknown> {
    const token = this.authService.getToken();

    if (token && !this.ehRotaDeAuth(req)) {
      return req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return req;
  }

  private ehRotaDeAuth(req: HttpRequest<unknown>): boolean {
    return req.url.startsWith('/api/auth');
  }

  private tratarNaoAutorizado(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (!this.refreshEmAndamento) {
      this.refreshEmAndamento = true;
      this.accessTokenRenovado$.next(null);

      return this.authService.refreshAccessToken().pipe(
        switchMap(response => {
          this.refreshEmAndamento = false;
          this.accessTokenRenovado$.next(response.accessToken);
          return next.handle(this.comToken(req));
        }),
        catchError(erro => {
          this.refreshEmAndamento = false;
          this.authService.logout();
          return throwError(() => erro);
        })
      );
    }

    return this.accessTokenRenovado$.pipe(
      filter(token => token !== null),
      take(1),
      switchMap(() => next.handle(this.comToken(req)))
    );
  }
}
