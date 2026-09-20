import { Environment } from '../app/core/models/environment.model';

export const environment: Environment = {
  production: true,
  logging: {
    minLevel: 'warn',
    telemetryUrl: null
  }
};
