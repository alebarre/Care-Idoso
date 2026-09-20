import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { LogLevel } from '../models/environment.model';

interface EntradaLog {
  timestamp: string;
  correlationId: string | null;
  nivel: LogLevel;
  mensagem: string;
  contexto?: Record<string, unknown>;
}

const ORDEM_NIVEL: Record<LogLevel, number> = {
  debug: 0,
  info: 1,
  warn: 2,
  error: 3
};

/**
 * Serviço central de logging do front-end. Nunca receber senha, token,
 * e-mail ou dado de saúde em `contexto` — apenas identificadores técnicos.
 */
@Injectable({ providedIn: 'root' })
export class LoggerService {

  private correlationIdAtual: string | null = null;

  definirCorrelationId(correlationId: string): void {
    this.correlationIdAtual = correlationId;
  }

  debug(mensagem: string, contexto?: Record<string, unknown>): void {
    this.registrar('debug', mensagem, contexto);
  }

  info(mensagem: string, contexto?: Record<string, unknown>): void {
    this.registrar('info', mensagem, contexto);
  }

  warn(mensagem: string, contexto?: Record<string, unknown>): void {
    this.registrar('warn', mensagem, contexto);
  }

  error(mensagem: string, contexto?: Record<string, unknown>): void {
    this.registrar('error', mensagem, contexto);
  }

  private registrar(nivel: LogLevel, mensagem: string, contexto?: Record<string, unknown>): void {
    if (ORDEM_NIVEL[nivel] < ORDEM_NIVEL[environment.logging.minLevel]) {
      return;
    }

    const entrada: EntradaLog = {
      timestamp: new Date().toISOString(),
      correlationId: this.correlationIdAtual,
      nivel,
      mensagem,
      contexto
    };

    if (!environment.production) {
      const metodoConsole = nivel === 'debug' ? 'log' : nivel;
      // eslint-disable-next-line no-console
      console[metodoConsole](`[${entrada.correlationId ?? 'sem-correlation-id'}]`, mensagem, contexto ?? '');
    }

    if (nivel === 'error' && environment.production && environment.logging.telemetryUrl) {
      this.enviarTelemetria(entrada);
    }
  }

  private enviarTelemetria(entrada: EntradaLog): void {
    try {
      const payload = new Blob([JSON.stringify(entrada)], { type: 'application/json' });
      navigator.sendBeacon?.(environment.logging.telemetryUrl as string, payload);
    } catch {
      // Falha ao enviar telemetria não deve interromper a aplicação.
    }
  }
}
