import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, filter, switchMap, take, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

/**
 * Anexa `Authorization: Bearer` às chamadas e renova o access token em 401.
 *
 * Fluxo de refresh: a primeira requisição que recebe 401 dispara
 * `refreshAccessToken()`; as demais aguardam em `accessTokenRenovado$` e são
 * reenviadas com o novo token (evita várias renovações simultâneas, que
 * invalidariam umas às outras por causa da rotação do refresh token).
 * Se a renovação falhar, faz logout e redireciona para o login.
 * Rotas `/api/auth/*` ficam de fora para não gerar laço de refresh.
 */
@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  /** Trava para garantir um único refresh por vez. */
  private refreshEmAndamento = false;
  /** Emite o novo access token para liberar requisições que esperavam o refresh; `null` = ainda renovando. */
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

  /** Endpoints de autenticação são públicos e não devem receber token nem disparar refresh. */
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
