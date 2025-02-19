import json

class DataLayer:
  def from_json(self, json_data):
    data = json.loads(json_data)
    return self(**data)
  
  def to_json(self):
    return json.dumps(self.__dict__)
  
  def __str__(self):
    return self.to_json()
  
  def __repr__(self):
    return self.to_json()
  
  def __eq__(self, other):
    return self.__dict__ == other.__dict__
  
  def __ne__(self, other):
    return not self.__eq__(other)
  
  def __hash__(self):
    return hash(self.__str__())
  
  def __getitem__(self, key):
    return self.__dict__[key]
  
  def __setitem__(self, key, value):
    self.__dict__[key] = value