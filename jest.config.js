module.exports = {
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/frontend/src/setupTests.ts'],
  transform: {
    '^.+\\.tsx?$': 'babel-jest',
  },
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/frontend/src/$1',
    '\\.(css|less|scss|svg|png|jpg)$': '<rootDir>/__mocks__/fileMock.js',
  },
  testMatch: [
    '<rootDir>/frontend/src/**/__tests__/**/*.{ts,tsx}',
    '<rootDir>/frontend/src/**/*.{spec,test}.{ts,tsx}',
  ],
  collectCoverageFrom: [
    'frontend/src/**/*.{ts,tsx}',
    '!frontend/src/**/*.d.ts',
    '!frontend/src/index.tsx',
    '!frontend/src/setupTests.ts',
  ],
};
