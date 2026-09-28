export interface Address {
  id: string;
  userId: string;
  name: string;
  phone: string;
  addressLine: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  isDefault: boolean;
  createdAt: number;
  updatedAt: number;
}
