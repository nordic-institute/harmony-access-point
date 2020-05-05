import {Injectable} from '@angular/core';
import {PropertiesService} from '../properties/support/properties.service';

@Injectable()
export class FileUploadValidatorService {

  constructor(private propertiesService: PropertiesService) {
  }

  public async validateSize(file: Blob): Promise<any> {
    let limit;
    try {
      limit = await this.getUploadSizeLimit();
    } catch (e) {
      console.log('Exception while reading upload size limit property:', e);
      // no error in case we cannot read property because the server will validate
      return ;
    }

    if (limit && file.size > limit) {
      throw new Error(`The file size ${file.size} exceeds the maximum size limit of ${limit}.`);
    }
  }

  private async getUploadSizeLimit(): Promise<number> {
    const res = await this.propertiesService.getUploadSizeLimitProperty();
    return +res.value;
  }

}

