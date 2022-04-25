import threading
import time

"""
Simple bound cache example, if cache is full wait until items are removed
"""
class BlockingCache(object, ):
  def __init__(self, max_items=10):
    self.cache = {}
    self.max_items = max_items
    self.cv = threading.Condition()

  def put(self, key, value):
      """
      Put item in cache, wait it cache is full
      :param key:
      :param value:
      :return:
      """
      with self.cv:
          self.cv.wait_for(lambda: self.size()  < self.max_items)
          self.cache[key] = value
          self.cv.notify_all()

  def head(self):
      """
      Get first key in cache
      :param key
      :return item | undefined
      """
      with self.cv:
        self.cv.wait_for(lambda: self.size() > 0)
        return list(self.cache.keys())[0]

  def size(self):
      """
      return size of cache
      :return int
      """
      return len(self.cache)

  def waitEmpty(self):
      """
      Block until cache is empty
      """
      with  self.cv:
        self.cv.wait_for(lambda :self.size() == 0)

  def peek(self, key):
      """
      Get item from cache, leaving item in cache
      :param key
      :return item | undefined
      """
      return self.cache.get(key)

  def remove(self, key):
      """
      Remove and return item from cache
      :param key
      :return item | error
      """
      with self.cv:
          removed = self.cache.pop(key)
          self.cv.notify_all()
          return removed

# Testing ...
if __name__ == '__main__':
    c = BlockingCache(max_items=10)
    def worker():
        while True:
            key = c.head()
            print(f'Working on {key}')
            time.sleep(1)
            print(f'Peek on {c.peek(key)}')
            removed = c.remove(key)
            print(f'Removed {key}')

    # spawn some threads to run worker
    threading.Thread(target=worker, daemon=True).start()

    for i in range(0, 30):
        print(f'Put {i}')
        c.put(i, {"num":i})

    c.waitEmpty()