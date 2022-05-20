export class PasswordPolicyRO {
  pattern: string;
  validationMessage: string;

  constructor (pattern: string, validationMessage: string) {
    this.pattern = pattern;
    this.validationMessage = validationMessage;
  }
}
