import { Environment } from '../app/core/models/environment.model';

export const environment: Environment = {
  production: false,
  logging: {
    minLevel: 'debug',
    telemetryUrl: null
  }
};
