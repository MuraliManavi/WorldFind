import { createApp } from './app';
import { env } from './config/env';
import { initializeFirebase } from './config/firebase';
import { logger } from './utils/logger';

const startServer = async () => {
  // Initialize Firebase Admin SDK
  initializeFirebase();

  const app = createApp();
  const port = env.PORT;

  const server = app.listen(port, '0.0.0.0', () => {
    logger.info(`==================================================`);
    logger.info(` WorldFind Backend Service is Running!`);
    logger.info(` Environment: ${env.NODE_ENV}`);
    logger.info(` Listening on: http://0.0.0.0:${port}`);
    logger.info(` Local Emulator Base URL: http://10.0.2.2:${port}/`);
    logger.info(`==================================================`);
  });

  // Graceful Shutdown
  const gracefulShutdown = (signal: string) => {
    logger.info(`Received ${signal}. Shutting down WorldFind Backend gracefully...`);
    server.close(() => {
      logger.info('HTTP server closed. Exiting process.');
      process.exit(0);
    });

    // Force exit after 10s timeout
    setTimeout(() => {
      logger.error('Forced shutdown after timeout.');
      process.exit(1);
    }, 10000);
  };

  process.on('SIGINT', () => gracefulShutdown('SIGINT'));
  process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));
};

startServer().catch((error) => {
  logger.error('Fatal error during WorldFind Backend startup:', error);
  process.exit(1);
});
