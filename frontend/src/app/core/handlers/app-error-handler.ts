import { ErrorHandler, Injectable, Injector } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { LoggerService } from '../services/logger.service';

@Injectable()
export class AppErrorHandler implements ErrorHandler {

  constructor(private injector: Injector) { }

  handleError(error: unknown): void {
    const loggerService = this.injector.get(LoggerService);

    if (error instanceof HttpErrorResponse) {
      loggerService.error('Erro HTTP não tratado', {
        status: error.status,
        url: error.url
      });
      return;
    }

    const mensagem = error instanceof Error ? error.message : 'Erro desconhecido na aplicação';
    loggerService.error(mensagem);
  }
}
