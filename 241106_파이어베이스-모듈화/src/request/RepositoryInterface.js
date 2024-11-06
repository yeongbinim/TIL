export default class RepositoryInterface {
  constructor() {
      if (new.target === RepositoryInterface) {
        throw new Error("RepositoryInterface 클래스는 인스턴스화할 수 없습니다.");
      }
      const interfaceMethods = Object.getOwnPropertyNames(RepositoryInterface.prototype)
        .filter(prop => typeof RepositoryInterface.prototype[prop] === 'function' && prop !== 'constructor');
      
      const overridedMethods = Object.getOwnPropertyNames(Object.getPrototypeOf(this))
        .filter(prop => typeof this[prop] === 'function' && prop !== 'constructor');
      
        interfaceMethods.forEach(method => {
        if (!overridedMethods.includes(method)) {
          throw new Error(`${method} 메서드를 오버라이드해야 합니다.`);
        }
      });
  }

  async save(obj) {}
  async findAll() {}
  async findBy(key, value) {}
  async deleteById(id) {}
  async updateById(id, obj) {}
}