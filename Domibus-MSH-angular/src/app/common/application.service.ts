import {Injectable} from '@angular/core';
import 'rxjs/add/operator/map';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Service meant to keep the current component in the outlet(set by the framework)
 */
@Injectable()
export class ApplicationService {
  private currentComponent: any;

  constructor () {
  }

  getCurrentComponent (): any {
    return this.currentComponent;
  }

  setCurrentComponent (value: any) {
    this.currentComponent = value;
  }

}
