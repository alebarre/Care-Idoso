/** Níveis de log em ordem crescente de severidade. */
export type LogLevel = 'debug' | 'info' | 'warn' | 'error';

/** Configuração de logging por ambiente. */
export interface LoggingConfig {
  /** Nível mínimo registrado; abaixo dele a entrada é descartada. */
  minLevel: LogLevel;
  /** Endpoint que recebe erros em produção; `null` desativa o envio. */
  telemetryUrl: string | null;
}

/** Contrato de `environment.ts` / `environment.prod.ts` (substituídos em build via fileReplacements). */
export interface Environment {
  production: boolean;
  logging: LoggingConfig;
}
