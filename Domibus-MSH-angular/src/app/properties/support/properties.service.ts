import {HttpClient, HttpParams} from '@angular/common/http';
import {AlertService} from 'app/common/alert/alert.service';
import {Injectable} from '@angular/core';

@Injectable()
export class PropertiesService {
  static readonly PROPERTIES_URL: string = 'rest/configuration/properties';

  regularExpressions: Map<string, RegExp> = new Map<string, RegExp>();

  constructor(private http: HttpClient, private alertService: AlertService) {
  }

  async loadPropertyTypes(): Promise<any> {
    const types = await this.http.get<any[]>('rest/configuration/properties/metadata/types').toPromise();
    const result = new Map(types.filter(el => el.regularExpression != null).map(i => [i.name, new RegExp(i.regularExpression)]));
    this.regularExpressions = result;
  }

  getProperties(searchString: string, showDomainProperties: boolean, pageSize: number = 10, offset: number = 0)
    : Promise<PropertyListModel> {
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

  async getProperty(propName: string, isDomain: boolean): Promise<PropertyModel> {
    const result = await this.getProperties(propName, isDomain, 1);
    if (result.count === 0) {
      return null;
    }
    if (result.count > 1) {
      throw new Error(`More than one value found for property ${propName}`);
    }
    return result.items[0];
  }

  updateProperty(prop: any, isDomain: boolean): Promise<void> {
    this.validateValue(prop);

    let value = prop.value;
    if (value === '') { // sanitize empty value: the api needs the body to be present, even if empty
      value = ' ';
    }

    return this.http.put(PropertiesService.PROPERTIES_URL + '/' + prop.name, value, {params: {isDomain: isDomain.toString()}})
      .map(() => {
      }).toPromise()
  }

  validateValue(prop) {
    const propType = prop.type;
    const regexp = this.regularExpressions.get(propType);
    if (!regexp) {
      return;
    }

    if (!regexp.test(prop.value)) {
      throw new Error(`Value '${prop.value}' for property '${prop.name}' is not of type '${prop.type}'`);
    }
  }

  async getUploadSizeLimitProperty(): Promise<PropertyModel> {
    return this.getProperty('domibus.file.upload.maxSize', false);
  }

  async getCsvMaxRowsProperty(): Promise<PropertyModel> {
    return this.getProperty('domibus.ui.csv.rows.max', true);
  }

}

export interface PropertyModel {
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

export interface PropertyListModel {
  count: number;
  items: PropertyModel[];
}
