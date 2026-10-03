import { Address } from '../../models/Address';
import { getFirestoreDb } from '../../config/firebase';
import { NotFoundError, BadRequestError } from '../../utils/errors';

const memoryAddressStore = new Map<string, Address[]>();

export class AddressService {
  private static validateIndianAddress(addressData: Partial<Address>): void {
    if (addressData.phone) {
      const cleanPhone = addressData.phone.replace(/[\s\-\+\(\)]/g, '');
      const digits = cleanPhone.slice(-10);
      if (digits.length !== 10 || !/^[6-9][0-9]{9}$/.test(digits)) {
        throw new BadRequestError('Invalid phone number. Please enter a valid 10-digit mobile number.');
      }
    }

    if (addressData.postalCode) {
      const cleanPin = addressData.postalCode.replace(/\s+/g, '');
      if (!/^[1-9][0-9]{5}$/.test(cleanPin)) {
        throw new BadRequestError('Invalid PIN code. Please enter a valid 6-digit Indian PIN code (e.g. 110001, 400001, 560001).');
      }
    }
  }

  public static async getAddresses(userId: string): Promise<Address[]> {
    const db = getFirestoreDb();
    if (!db) {
      return memoryAddressStore.get(userId) || [];
    }

    const snapshot = await db.collection('users').doc(userId).collection('addresses').orderBy('createdAt', 'desc').get();
    return snapshot.docs.map((doc: { data: () => unknown }) => doc.data() as Address);
  }

  public static async addAddress(userId: string, addressData: Partial<Address>): Promise<Address> {
    this.validateIndianAddress(addressData);

    const now = Date.now();
    const addressId = `addr_${now}_${Math.random().toString(36).substring(2, 7)}`;
    const addresses = await this.getAddresses(userId);

    const isDefault = addressData.isDefault || addresses.length === 0;

    const newAddress: Address = {
      id: addressId,
      userId,
      name: addressData.name || 'Recipient',
      phone: addressData.phone || '',
      addressLine: addressData.addressLine || '',
      city: addressData.city || '',
      state: addressData.state || '',
      postalCode: addressData.postalCode || '',
      country: addressData.country || 'India',
      isDefault,
      createdAt: now,
      updatedAt: now,
    };

    const db = getFirestoreDb();
    if (!db) {
      if (isDefault) {
        addresses.forEach((a) => (a.isDefault = false));
      }
      addresses.unshift(newAddress);
      memoryAddressStore.set(userId, addresses);
      return newAddress;
    }

    const batch = db.batch();
    if (isDefault) {
      const existing = await db.collection('users').doc(userId).collection('addresses').where('isDefault', '==', true).get();
      existing.docs.forEach((doc: { ref: FirebaseFirestore.DocumentReference }) => batch.update(doc.ref, { isDefault: false, updatedAt: now }));
    }

    const docRef = db.collection('users').doc(userId).collection('addresses').doc(addressId);
    batch.set(docRef, newAddress);
    await batch.commit();

    return newAddress;
  }

  public static async updateAddress(userId: string, addressId: string, addressData: Partial<Address>): Promise<Address> {
    this.validateIndianAddress(addressData);

    const addresses = await this.getAddresses(userId);
    const existing = addresses.find((a) => a.id === addressId);
    if (!existing) {
      throw new NotFoundError(`Address ${addressId} not found.`);
    }

    const now = Date.now();
    const updated: Address = {
      ...existing,
      ...addressData,
      id: addressId,
      userId,
      updatedAt: now,
    };

    const db = getFirestoreDb();
    if (!db) {
      if (updated.isDefault) {
        addresses.forEach((a) => (a.isDefault = a.id === addressId));
      }
      const index = addresses.findIndex((a) => a.id === addressId);
      if (index !== -1) addresses[index] = updated;
      memoryAddressStore.set(userId, addresses);
      return updated;
    }

    const batch = db.batch();
    if (updated.isDefault) {
      const currentDefaults = await db.collection('users').doc(userId).collection('addresses').where('isDefault', '==', true).get();
      currentDefaults.docs.forEach((doc: { ref: FirebaseFirestore.DocumentReference }) => batch.update(doc.ref, { isDefault: false, updatedAt: now }));
    }

    const docRef = db.collection('users').doc(userId).collection('addresses').doc(addressId);
    batch.set(docRef, updated);
    await batch.commit();

    return updated;
  }

  public static async deleteAddress(userId: string, addressId: string): Promise<void> {
    const db = getFirestoreDb();
    if (!db) {
      const addresses = memoryAddressStore.get(userId) || [];
      memoryAddressStore.set(
        userId,
        addresses.filter((a) => a.id !== addressId)
      );
      return;
    }

    await db.collection('users').doc(userId).collection('addresses').doc(addressId).delete();
  }

  public static async setDefaultAddress(userId: string, addressId: string): Promise<Address[]> {
    return this.updateAddress(userId, addressId, { isDefault: true }).then(() => this.getAddresses(userId));
  }
}
