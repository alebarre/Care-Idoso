import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoggerService } from '../services/logger.service';

/** Mesmo nome de header lido pelo `CorrelationIdFilter` do backend. */
const HEADER_CORRELATION_ID = 'X-Correlation-Id';

/**
 * Gera um UUID por requisição e o envia em `X-Correlation-Id`, permitindo
 * rastrear a mesma chamada nos logs do front e do backend. O id também é
 * registrado no `LoggerService`, que o inclui nas próximas entradas de log.
 */
@Injectable()
export class CorrelationIdInterceptor implements HttpInterceptor {

  constructor(private loggerService: LoggerService) { }

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const correlationId = crypto.randomUUID();
    this.loggerService.definirCorrelationId(correlationId);

    const reqComCorrelationId = req.clone({
      setHeaders: {
        [HEADER_CORRELATION_ID]: correlationId
      }
    });

    return next.handle(reqComCorrelationId);
  }
}
