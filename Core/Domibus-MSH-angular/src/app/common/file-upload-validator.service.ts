import {Injectable} from '@angular/core';
import {PropertiesService} from '../properties/support/properties.service';

@Injectable()
export class FileUploadValidatorService {

  constructor(private propertiesService: PropertiesService) {
  }

  public async validateFileSize(file: Blob): Promise<any> {
    await this.validateSize(file.size, 'file');
  }

  public async validateStringSize(value: string) {
    await this.validateSize(value.length);
  }

  private async validateSize(size: number, type: string = 'value') {
    let limit;
    try {
      limit = await this.getUploadSizeLimit();
    } catch (e) {
      console.log('Exception while reading upload size limit property:', e);
      // no error in case we cannot read property because the server will validate
      return;
    }

    if (limit && size > limit) {
      throw new Error(`The ${type} size ${size} exceeds the maximum size limit of ${limit}.`);
    }
  }

  private async getUploadSizeLimit(): Promise<number> {
    const res = await this.propertiesService.getUploadSizeLimitProperty();
    return +res.value;
  }

}

