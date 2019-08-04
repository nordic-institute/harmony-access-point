export class MessagesRequestRO {

  public source: string;
  public jmsType: string;
  public fromDate: Date;
  public toDate: Date;
  public selector: string;

  constructor() {
    this.source = null;
    this.jmsType = null;
    this.fromDate = null;
    this.toDate = null;
    this.selector = null;
  }

}
