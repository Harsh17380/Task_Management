export interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  status: boolean;
  companyId?: number;
  companyName?: string;
}
