import { Environment } from '../app/core/models/environment.model';

/** Produção: só warn/error; ative `telemetryUrl` para enviar erros ao backend de telemetria. */
export const environment: Environment = {
  production: true,
  logging: {
    minLevel: 'warn',
    telemetryUrl: null
  }
};
