import {HttpClient, HttpParams} from '@angular/common/http';
import {AlertService} from 'app/common/alert/alert.service';
import {Injectable} from '@angular/core';

@Injectable()
export class PropertiesService {

  static readonly PROPERTIES_URL: string = 'rest/configuration/properties';

  constructor(private http: HttpClient, private alertService: AlertService) {
  }

  getProperties(searchString: string, showDomainProperties: boolean, pageSize: number, offset: number): Promise<PropertyListModel> {
    const searchParams = new HttpParams();
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

    return this.http.get<PropertyListModel>(PropertiesService.PROPERTIES_URL, {params: searchParams})
      .toPromise()
      .catch(err => this.alertService.handleError(err));
  }

  updateProperty(name: any, isDomain: boolean, value: any): Promise<void> {
    return this.http.put(PropertiesService.PROPERTIES_URL + '/' + name, value, {params: {isDomain: isDomain.toString()}})
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
