/** @type {import('ts-jest').JestConfigWithTsJest} */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  testMatch: ['**/tests/**/*.test.ts'],
  transform: {
    '^.+\\.tsx?$': ['ts-jest', {
      tsconfig: {
        module: 'CommonJS',
        moduleResolution: 'node',
        target: 'ES2022',
        esModuleInterop: true,
        strict: true,
        skipLibCheck: true
      }
    }]
  },
  moduleFileExtensions: ['ts', 'js', 'json', 'node'],
  clearMocks: true,
  resetMocks: true,
  restoreMocks: true,
  verbose: true
};
