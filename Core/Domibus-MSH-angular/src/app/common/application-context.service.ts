import {Injectable, Injector} from '@angular/core';
import 'rxjs/add/operator/map';
import BaseListComponent from './mixins/base-list.component';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Service meant to keep the current component in the outlet(set by the framework)
 */
@Injectable()
export class ApplicationContextService {
  private currentComponent: any;

  constructor (public injector: Injector) {
  }

  getCurrentComponent (): BaseListComponent<any> {
    return this.currentComponent;
  }

  setCurrentComponent (value: BaseListComponent<any>) {
    this.currentComponent = value;
  }

}
