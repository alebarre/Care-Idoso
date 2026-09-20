import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoggerService } from '../services/logger.service';

const HEADER_CORRELATION_ID = 'X-Correlation-Id';

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
