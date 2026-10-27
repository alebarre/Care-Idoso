import { Environment } from '../app/core/models/environment.model';

/** Desenvolvimento: log detalhado no console, sem telemetria. */
export const environment: Environment = {
  production: false,
  logging: {
    minLevel: 'debug',
    telemetryUrl: null
  }
};
