import {HttpClient, HttpParams} from '@angular/common/http';
import {AlertService} from 'app/common/alert/alert.service';
import {Injectable} from '@angular/core';

@Injectable()
export class PropertiesService {

  static readonly PROPERTIES_URL: string = 'rest/configuration/properties';

  constructor (private http: HttpClient, private alertService: AlertService) {
  }

  getProperties (searchString: string, showDomainProperties: boolean, pageSize: number = 10, offset: number = 0): Promise<PropertyListModel> {
    let searchParams = new HttpParams();
    if (searchString && searchString.trim()) {
      searchParams = searchParams.append('name', searchString.trim());
    }
    if (!showDomainProperties) {
      searchParams = searchParams.append('showDomain', (!!showDomainProperties).toString());
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

  updateProperty (prop: any, isDomain: boolean): Promise<void> {
    this.validateValue(prop);

    let value = prop.value;
    if (value === '') { // sanitize empty value: the api needs the body to be present, even if empty
      value = ' ';
    }

    return this.http.put(PropertiesService.PROPERTIES_URL + '/' + prop.name, value, {params: {isDomain: isDomain.toString()}})
      .map(() => {
      }).toPromise()
  }

  validateValue (prop) {
    const propType = prop.type;
    const regexp = this.regularExpressions[propType];
    if (!regexp) {
      return;
    }

    if (!regexp.test(prop.value)) {
      throw new Error(`Value '${prop.value}' for property '${prop.name}' is not of type '${prop.type}'`);
    }
  }

  regularExpressions = {
    'cron': /^(\*|([0-9]|1[0-9]|2[0-9]|3[0-9]|4[0-9]|5[0-9])|\*\/([0-9]|1[0-9]|2[0-9]|3[0-9]|4[0-9]|5[0-9])) (\*|([0-9]|1[0-9]|2[0-3])|\*\/([0-9]|1[0-9]|2[0-3])) (\*|([1-9]|1[0-9]|2[0-9]|3[0-1])|\*\/([1-9]|1[0-9]|2[0-9]|3[0-1])) (\*|([1-9]|1[0-2])|\*\/([1-9]|1[0-2])) (\*|([0-6])|\*\/([0-6]))$/,
    'concurrency': /^(\d+(\-\d+)*)$/,
    'numeric': /^(\d+)$/,
    'boolean': /^(true|false)$/,
  };

}

export class PropertyModel {
  value: string;
  name: string;
  type: string;
  usageText: string;
  isComposable: boolean;
  withFallback: boolean;
  clusterAware: boolean;
  section: string;
  description: string;
  module: string;
  writable: boolean;
  encrypted: boolean;
}

export class PropertyListModel {
  count: number;
  items: PropertyModel[];
}
