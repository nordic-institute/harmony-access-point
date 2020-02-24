import {HttpClient, HttpParams} from '@angular/common/http';
import {AlertService} from 'app/common/alert/alert.service';
import {Injectable} from '@angular/core';

@Injectable()
export class PropertiesService {

  static readonly PROPERTIES_URL: string = 'rest/configuration/properties';

  constructor(private http: HttpClient, private alertService: AlertService) {
  }

  getProperties(searchString: string, showDomainProperties: boolean, pageSize: number, offset: number): Promise<PropertyListModel> {
    let searchParams = new HttpParams();
    if (searchString && searchString.trim()) {
      searchParams = searchParams.append('name', searchString.trim());
    }
    if (!showDomainProperties) {
      searchParams = searchParams.append('showDomain', showDomainProperties.toString());
    }
    if (pageSize) {
      searchParams = searchParams.append('pageSize', pageSize.toString());
    }
    if (offset) {
      searchParams = searchParams.append('page', offset.toString());
    }

    return this.http.get<PropertyListModel>(PropertiesService.PROPERTIES_URL, {params: searchParams})
      .toPromise()
  }

  updateProperty(name: any, isDomain: boolean, value: any): Promise<void> {
    if (value === '') value = ' ';
    return this.http.put(PropertiesService.PROPERTIES_URL + '/' + name, value, {params: {isDomain: isDomain.toString()}})
      .map(() => {
      })
      .toPromise()
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
