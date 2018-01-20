let people = [{"name": "Alice", "age": 24}, {"name": "Anna", "age": 28}];

puts(people[0]["name"]);
puts(people[0]["age"]);
puts(people[1]["age"] + people[0]["age"]);

let getName = fn(person) { person["name"]; };

puts(getName(people[0]))
puts(getName(people[1]))
