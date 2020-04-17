import {Injectable, Injector} from '@angular/core';
import 'rxjs/add/operator/map';

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

  getCurrentComponent (): any {
    return this.currentComponent;
  }

  setCurrentComponent (value: any) {
    this.currentComponent = value;
  }

}
