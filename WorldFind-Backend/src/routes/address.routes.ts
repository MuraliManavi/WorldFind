import { Router } from 'express';
import { AddressController } from '../controllers/address.controller';
import { requireFirebaseAuth } from '../middleware/auth.middleware';

const router = Router();

router.get('/addresses', requireFirebaseAuth, AddressController.getAddresses);
router.post('/addresses', requireFirebaseAuth, AddressController.addAddress);
router.patch('/addresses/:addressId', requireFirebaseAuth, AddressController.updateAddress);
router.delete('/addresses/:addressId', requireFirebaseAuth, AddressController.deleteAddress);
router.patch('/addresses/:addressId/default', requireFirebaseAuth, AddressController.setDefault);

export default router;
