import {RoutingCriteriaEntry} from './routingcriteriaentry';

export class BackendFilterEntry {

  public static routingCriteriaNames = ['from', 'to', 'action', 'service'];

  from: RoutingCriteriaEntry;
  to: RoutingCriteriaEntry;
  action: RoutingCriteriaEntry;
  service: RoutingCriteriaEntry;

  public constructor(public entityId: number,
                     public index: number,
                     public backendName: string,
                     public routingCriterias: Array<RoutingCriteriaEntry>,
                     public persisted: boolean) {
    this.initRoutingCriteria();
  }

  public initRoutingCriteria() {
    this.from = this.getRoutingCriteria('from');
    this.to = this.getRoutingCriteria('to');
    this.action = this.getRoutingCriteria('action');
    this.service = this.getRoutingCriteria('service');
  }

  // public fromPropertiesToArray() {
  //   this.routingCriterias = BackendFilterEntry.routingCriteriaNames.map(name => this[name]);
  // }

  public getRoutingCriteria(property: string): RoutingCriteriaEntry {
    for (let i = 0; i < this.routingCriterias.length; i++) {
      if (this.routingCriterias[i].name == property) {
        return this.routingCriterias[i];
      }
    }
    return null;
  }

}
