import {Headers, Http, URLSearchParams, Response} from '@angular/http';
import {AlertService} from 'app/common/alert/alert.service';
import {Injectable} from '@angular/core';
import {DomainService} from '../security/domain.service';
import {SecurityService} from '../security/security.service';

@Injectable()
export class PropertiesService {

  static readonly PROPERTIES_URL: string = 'rest/configuration/properties';

  constructor(private http: Http, private alertService: AlertService, private securityService: SecurityService, private domainService: DomainService) {
  }

  async isIncludeSuperPropertiesVisible (): Promise<boolean> {
    const isMultiDomain = await this.domainService.isMultiDomain().toPromise();
    return isMultiDomain && this.securityService.isCurrentUserSuperAdmin();
  }

  getProperties(searchString: string, includeSuperProperties: boolean, pageSize: number, offset: number): Promise<PropertyListModel> {
    const searchParams = new URLSearchParams();
    if (searchString && searchString.trim()) {
      searchParams.set('name', searchString.trim());
    }
    if (includeSuperProperties) {
      searchParams.set('includeSuperProperties', includeSuperProperties.toString());
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

  updateProperty(name: any, value: any): Promise<void> {
    return this.http.put(PropertiesService.PROPERTIES_URL + '/' + name, value)
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
