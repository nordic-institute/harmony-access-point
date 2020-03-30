import {Injectable} from '@angular/core';
import 'rxjs/add/operator/map';

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
