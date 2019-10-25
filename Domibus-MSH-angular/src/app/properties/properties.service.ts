import {Headers, Http, URLSearchParams, Response} from '@angular/http';
import {AlertService} from 'app/common/alert/alert.service';
import {Injectable} from '@angular/core';
import {DomainService} from '../security/domain.service';
import {SecurityService} from '../security/security.service';

@Injectable()
export class PropertiesService {

  static readonly PROPERTIES_URL: string = 'rest/configuration/properties';

  constructor(private http: Http, private alertService: AlertService) {
  }

  getProperties(searchString: string, showDomainProperties: boolean, pageSize: number, offset: number): Promise<PropertyListModel> {
    const searchParams = new URLSearchParams();
    if (searchString && searchString.trim()) {
      searchParams.set('name', searchString.trim());
    }
    if (showDomainProperties) {
      searchParams.set('showDomain', showDomainProperties.toString());
    }
    if (pageSize) {
      searchParams.set('pageSize', pageSize.toString());
    }
    if (offset) {
      searchParams.set('page', offset.toString());
    }

    return this.http.get(PropertiesService.PROPERTIES_URL, {search: searchParams})
      .map(this.extractData)
      .toPromise()
      .catch(err => this.alertService.handleError(err));
  }

  private extractData(res: Response) {
    let body = res.json();
    return body || {};
  }

  updateProperty(name: any, isDomain: boolean, value: any): Promise<void> {
    return this.http.put(PropertiesService.PROPERTIES_URL + '/' + name, value, {params: {isDomain: isDomain}})
      .map(() => {
      })
      .toPromise()
      .catch(err => this.alertService.handleError(err));
  }
}

export class PropertyModel {
  value: string;
  metadata: any;
}

export class PropertyListModel {
  count: number;
  items: PropertyModel[];
}
