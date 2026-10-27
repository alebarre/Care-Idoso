import { ErrorHandler, Injectable, Injector } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { LoggerService } from '../services/logger.service';

/**
 * `ErrorHandler` global: encaminha erros não tratados ao `LoggerService` sem
 * expor dados do usuário (só status/URL em erros HTTP; mensagem nos demais).
 */
@Injectable()
export class AppErrorHandler implements ErrorHandler {

  // Injector em vez de injeção direta evita dependência circular na criação do ErrorHandler.
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
