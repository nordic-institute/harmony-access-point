/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * An interface for the list
 * */
export interface MultipleItemsResponse {
  message: string;
  issues: ResponseItemDetail[];
}

export interface ResponseItemDetail {
  message: string;
  level: string;
}

export function instanceOfMultipleItemsResponse(object: any): object is MultipleItemsResponse {
  if (typeof object == 'string') return false;
  return 'message' in object && 'issues' in object && Array.isArray(object.issues);
}

