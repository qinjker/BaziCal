/**
 * 响应工具单元测试
 */

import { success, error, unauthorized, forbidden, notFound, badRequest } from '../src/utils/response';

describe('Response Utils', () => {
  describe('success', () => {
    it('should return success response with default message', () => {
      const response = success();

      expect(response).toEqual({
        code: 0,
        message: 'Success',
        data: undefined,
      });
    });

    it('should return success response with custom data', () => {
      const data = { id: 1, name: 'test' };
      const response = success(data, 'Created');

      expect(response).toEqual({
        code: 0,
        message: 'Created',
        data,
      });
    });

    it('should return success response with custom message', () => {
      const response = success({ key: 'value' }, 'Custom message');

      expect(response.code).toBe(0);
      expect(response.message).toBe('Custom message');
      expect(response.data).toEqual({ key: 'value' });
    });
  });

  describe('error', () => {
    it('should return error response with default code', () => {
      const response = error('Something went wrong');

      expect(response).toEqual({
        code: 1,
        message: 'Something went wrong',
      });
    });

    it('should return error response with custom code', () => {
      const response = error('Custom error', 500);

      expect(response.code).toBe(500);
      expect(response.message).toBe('Custom error');
    });
  });

  describe('unauthorized', () => {
    it('should return 401 response with default message', () => {
      const response = unauthorized();

      expect(response).toEqual({
        code: 401,
        message: 'Unauthorized',
      });
    });

    it('should return 401 response with custom message', () => {
      const response = unauthorized('Invalid token');

      expect(response.code).toBe(401);
      expect(response.message).toBe('Invalid token');
    });
  });

  describe('forbidden', () => {
    it('should return 403 response with default message', () => {
      const response = forbidden();

      expect(response).toEqual({
        code: 403,
        message: 'Forbidden',
      });
    });
  });

  describe('notFound', () => {
    it('should return 404 response with default message', () => {
      const response = notFound();

      expect(response).toEqual({
        code: 404,
        message: 'Not Found',
      });
    });
  });

  describe('badRequest', () => {
    it('should return 400 response with default message', () => {
      const response = badRequest();

      expect(response).toEqual({
        code: 400,
        message: 'Bad Request',
      });
    });

    it('should return 400 response with custom message', () => {
      const response = badRequest('Invalid input');

      expect(response.code).toBe(400);
      expect(response.message).toBe('Invalid input');
    });
  });
});