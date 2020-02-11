import {Injectable} from '@angular/core';
import {PropertiesService} from '../properties/properties.service';

@Injectable()
export class FileUploadValidatorService {

  constructor(private propertiesService: PropertiesService) {
  }

  public async validateSize(file: Blob): Promise<any> {
    const limit = await this.getUploadSizeLimit();
    if (file.size > limit) {
      throw new Error('The file exceeds the maximum size limit.');
    }
  }

  private async getUploadSizeLimit(): Promise<number> {
    const res = await this.propertiesService.getProperties('domibus.file.upload.max.size', true, 1, 0)
    return +res.items[0].value;
  }

}

