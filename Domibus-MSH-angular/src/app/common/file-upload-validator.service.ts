import {Injectable} from '@angular/core';
import {PropertiesService} from '../properties/properties.service';

@Injectable()
export class FileUploadValidatorService {

  constructor(private propertiesService: PropertiesService) {
  }

  public async validateSize(file: Blob): Promise<any> {
    try {
      const limit = await this.getUploadSizeLimit();
      if (file.size > limit) {
        throw new Error('The file exceeds the maximum size limit.');
      }
    } catch (e) {
      //no error in case we cannot read property because the server will validate
    }
  }

  private async getUploadSizeLimit(): Promise<number> {
    const res = await this.propertiesService.getProperties('domibus.file.upload.maxSize', false);
    return +res.items[0].value;
  }

}

