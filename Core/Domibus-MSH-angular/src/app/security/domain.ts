export class Domain {
  code: string;
  name: string;
  active: boolean;

  constructor(code: string, name: string) {
    this.code = code;
    this.name = name;
  }
}
