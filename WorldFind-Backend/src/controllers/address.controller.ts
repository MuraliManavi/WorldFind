import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from '../middleware/auth.middleware';
import { AddressService } from '../services/address/address.service';

function getStringParam(param: string | string[] | undefined): string {
  if (Array.isArray(param)) return param[0] || '';
  return String(param || '');
}

export class AddressController {
  public static async getAddresses(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const addresses = await AddressService.getAddresses(userId);
      res.status(200).json({ success: true, data: addresses });
    } catch (error) {
      next(error);
    }
  }

  public static async addAddress(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const address = await AddressService.addAddress(userId, req.body);
      res.status(201).json({ success: true, data: address });
    } catch (error) {
      next(error);
    }
  }

  public static async updateAddress(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const addressId = getStringParam(req.params.addressId);
      const address = await AddressService.updateAddress(userId, addressId, req.body);
      res.status(200).json({ success: true, data: address });
    } catch (error) {
      next(error);
    }
  }

  public static async deleteAddress(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const addressId = getStringParam(req.params.addressId);
      await AddressService.deleteAddress(userId, addressId);
      res.status(200).json({ success: true, message: 'Address deleted successfully.' });
    } catch (error) {
      next(error);
    }
  }

  public static async setDefault(req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.uid || 'dev_user_001';
      const addressId = getStringParam(req.params.addressId);
      const addresses = await AddressService.setDefaultAddress(userId, addressId);
      res.status(200).json({ success: true, data: addresses });
    } catch (error) {
      next(error);
    }
  }
}
