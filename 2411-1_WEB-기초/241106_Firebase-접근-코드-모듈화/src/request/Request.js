import RepositoryInterface from "./RepositoryInterface.js";

export default class Request extends RepositoryInterface {
  #collectionName;
  #baseUrl = "https://crud-4xwhswem4q-uc.a.run.app";

  constructor(collectionName) {
    super();
    this.#collectionName = collectionName;
  }

  async save(obj) {
    const response = await fetch(`${this.#baseUrl}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        collectionName: this.#collectionName,
        obj: obj
      })
    });
    return response.json();
  }

  async findAll() {
    const response = await fetch(`${this.#baseUrl}?collectionName=${this.#collectionName}`);
    return response.json();
  }

  async findBy(key, value) {
    const response = await fetch(`${this.#baseUrl}?collectionName=${this.#collectionName}&key=${encodeURIComponent(key)}&value=${encodeURIComponent(value)}`);
    return response.json();
  }
  
  async updateById(id, obj) {
    const response = await fetch(`${this.#baseUrl}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        collectionName: this.#collectionName,
        id: id,
        obj: obj
      })
    });
    return response.json();
  }

  async deleteById(id) {
    const response = await fetch(`${this.#baseUrl}?collectionName=${this.#collectionName}&id=${id}`, {
        method: 'DELETE'
    });
    return response.json();
  }
}
