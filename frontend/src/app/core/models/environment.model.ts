export type LogLevel = 'debug' | 'info' | 'warn' | 'error';

export interface LoggingConfig {
  minLevel: LogLevel;
  telemetryUrl: string | null;
}

export interface Environment {
  production: boolean;
  logging: LoggingConfig;
}
