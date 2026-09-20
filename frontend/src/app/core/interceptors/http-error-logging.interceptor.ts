import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LoggerService } from '../services/logger.service';

/**
 * Loga todo erro HTTP de forma anonimizada, mesmo quando o componente
 * chamador também trata o erro localmente (ex.: toast de mensagem amigável).
 */
@Injectable()
export class HttpErrorLoggingInterceptor implements HttpInterceptor {

  constructor(private loggerService: LoggerService) { }

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((erro: HttpErrorResponse) => {
        this.loggerService.error('Erro HTTP', {
          status: erro.status,
          url: erro.url
        });
        return throwError(() => erro);
      })
    );
  }
}
