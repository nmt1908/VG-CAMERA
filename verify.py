import json

data = [
  {
    "url": "camera-storage/047409-250526-143011/photo-0.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23937111111111,
      "long": 106.25914083333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-1.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23937111111111,
      "long": 106.25914083333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-2.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23937111111111,
      "long": 106.25914083333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-3.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23937111111111,
      "long": 106.25914083333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-4.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23937111111111,
      "long": 106.25914083333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-5.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239386111111111,
      "long": 106.25920833333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-6.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239386111111111,
      "long": 106.25920833333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-7.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239386111111111,
      "long": 106.25920833333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-8.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239386111111111,
      "long": 106.25920833333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-9.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239386111111111,
      "long": 106.25920833333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-10.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239592777777776,
      "long": 106.25937027777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-11.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239591666666666,
      "long": 106.2592475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-12.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-13.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-14.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-15.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-16.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-17.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-18.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-19.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-20.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-21.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-22.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-23.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-24.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-25.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-26.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-27.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-28.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-29.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-30.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-31.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238643055555555,
      "long": 106.25840638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-32.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239538333333332,
      "long": 106.25929444444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-33.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239538333333332,
      "long": 106.25929444444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-34.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239538333333332,
      "long": 106.25929444444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-35.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239538333333332,
      "long": 106.25929444444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-36.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239538333333332,
      "long": 106.25929444444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-37.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239538333333332,
      "long": 106.25929444444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-38.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239610833333332,
      "long": 106.25934916666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-39.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239610833333332,
      "long": 106.25934916666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-60.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-61.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-62.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-63.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-64.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-65.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-66.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-67.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-68.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-69.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-70.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-71.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-72.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-73.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-74.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-75.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-76.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-77.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-78.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-79.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239271944444443,
      "long": 106.259315
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-80.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239271944444443,
      "long": 106.259315
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-81.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239271944444443,
      "long": 106.259315
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-82.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239271944444443,
      "long": 106.259315
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-83.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239271944444443,
      "long": 106.259315
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-84.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239271944444443,
      "long": 106.259315
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-85.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239271944444443,
      "long": 106.259315
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-86.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239271944444443,
      "long": 106.259315
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-87.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239271944444443,
      "long": 106.259315
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-88.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239271944444443,
      "long": 106.259315
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-89.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239271944444443,
      "long": 106.259315
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-90.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239271944444443,
      "long": 106.259315
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-91.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238568055555556,
      "long": 106.25842777777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-92.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238568055555556,
      "long": 106.25842777777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-93.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238518888888889,
      "long": 106.25826194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-94.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238518888888889,
      "long": 106.25826194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-95.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238518888888889,
      "long": 106.25826194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-96.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238518888888889,
      "long": 106.25826194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-97.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238518888888889,
      "long": 106.25826194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-98.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238518888888889,
      "long": 106.25826194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-99.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238518888888889,
      "long": 106.25826194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-40.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239610833333332,
      "long": 106.25934916666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-41.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239610833333332,
      "long": 106.25934916666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-42.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239610833333332,
      "long": 106.25934916666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-43.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239610833333332,
      "long": 106.25934916666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-44.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239610833333332,
      "long": 106.25934916666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-45.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-46.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-47.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-48.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-49.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-50.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-51.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-52.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-53.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-54.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-55.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-56.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-57.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-58.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-59.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239628888888888,
      "long": 106.2593475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-100.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239629444444443,
      "long": 106.25933361111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-101.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239629444444443,
      "long": 106.25933361111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-102.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239629444444443,
      "long": 106.25933361111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-103.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239629444444443,
      "long": 106.25933361111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-104.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239629444444443,
      "long": 106.25933361111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-105.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239629444444443,
      "long": 106.25933361111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-106.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239629444444443,
      "long": 106.25933361111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-107.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239629444444443,
      "long": 106.25933361111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-108.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239630833333333,
      "long": 106.25939111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-109.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239630833333333,
      "long": 106.25939111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-110.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239630833333333,
      "long": 106.25939111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-111.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239630833333333,
      "long": 106.25939111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-112.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239630833333333,
      "long": 106.25939111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-113.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239630833333333,
      "long": 106.25939111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-114.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239630833333333,
      "long": 106.25939111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-115.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239630833333333,
      "long": 106.25939111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-116.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239630833333333,
      "long": 106.25939111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-117.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239630833333333,
      "long": 106.25939111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-118.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-119.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-120.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-121.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-122.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-123.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-124.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-125.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-126.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-127.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-128.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-129.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-130.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-131.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-132.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-133.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-134.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-135.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-136.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-137.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-138.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-139.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-140.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-141.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-142.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-143.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-144.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-145.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-146.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-147.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-148.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-149.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-150.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-151.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-152.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-153.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-154.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-155.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-156.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-157.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239593055555554,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-158.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-159.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-160.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-161.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-162.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-163.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-164.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-165.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-166.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-167.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-168.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-169.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-170.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-171.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-172.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-173.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-174.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-175.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240751666666666,
      "long": 106.26416611111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-176.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-177.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-178.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-179.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-180.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-181.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-182.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-183.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-184.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-185.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-186.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-187.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-188.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-189.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-190.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.240031388888887,
      "long": 106.25864972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-191.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-192.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-193.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-194.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-195.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-196.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-197.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-198.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-199.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-200.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-201.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-202.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-203.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-204.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-205.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-206.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-207.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-208.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238982777777776,
      "long": 106.25899111111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-209.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238769166666666,
      "long": 106.25912805555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-210.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238769166666666,
      "long": 106.25912805555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-211.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238769166666666,
      "long": 106.25912805555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-212.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238769166666666,
      "long": 106.25912805555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-213.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238769166666666,
      "long": 106.25912805555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-214.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238769166666666,
      "long": 106.25912805555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-215.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238769166666666,
      "long": 106.25912805555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-216.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238827222222222,
      "long": 106.25909583333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-217.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238827222222222,
      "long": 106.25909583333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-218.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238827222222222,
      "long": 106.25909583333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-219.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238827222222222,
      "long": 106.25909583333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-220.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238613333333333,
      "long": 106.25848972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-221.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238613333333333,
      "long": 106.25848972222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-222.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238990833333332,
      "long": 106.25922638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-223.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238990833333332,
      "long": 106.25922638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-224.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238990833333332,
      "long": 106.25922638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-225.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238990833333332,
      "long": 106.25922638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-226.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23946611111111,
      "long": 106.25901861111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-227.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23946611111111,
      "long": 106.25901861111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-228.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23946611111111,
      "long": 106.25901861111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-229.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23946611111111,
      "long": 106.25901861111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-230.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23946611111111,
      "long": 106.25901861111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-231.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23946611111111,
      "long": 106.25901861111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-232.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23946611111111,
      "long": 106.25901861111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-233.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23946611111111,
      "long": 106.25901861111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-234.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23946611111111,
      "long": 106.25901861111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-235.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23946611111111,
      "long": 106.25901861111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-236.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23946611111111,
      "long": 106.25901861111112
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-237.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239544444444444,
      "long": 106.25928555555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-238.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239544444444444,
      "long": 106.25928555555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-239.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239544444444444,
      "long": 106.25928555555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-240.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239544444444444,
      "long": 106.25928555555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-241.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239544444444444,
      "long": 106.25928555555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-242.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239544444444444,
      "long": 106.25928555555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-243.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239544444444444,
      "long": 106.25928555555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-244.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239544444444444,
      "long": 106.25928555555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-245.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239544444444444,
      "long": 106.25928555555555
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-246.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635,
      "long": 106.25936388888888
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-247.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635,
      "long": 106.25936388888888
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-248.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635,
      "long": 106.25936388888888
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-249.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635,
      "long": 106.25936388888888
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-250.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635,
      "long": 106.25936388888888
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-251.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635,
      "long": 106.25936388888888
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-252.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635,
      "long": 106.25936388888888
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-253.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239618888888888,
      "long": 106.25938472222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-254.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239618888888888,
      "long": 106.25938472222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-255.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239618888888888,
      "long": 106.25938472222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-256.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239618888888888,
      "long": 106.25938472222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-257.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239618888888888,
      "long": 106.25938472222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-258.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239618888888888,
      "long": 106.25938472222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-259.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239618888888888,
      "long": 106.25938472222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-260.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239618888888888,
      "long": 106.25938472222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-261.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239618888888888,
      "long": 106.25938472222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-262.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239618888888888,
      "long": 106.25938472222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-263.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239618888888888,
      "long": 106.25938472222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-264.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239618888888888,
      "long": 106.25938472222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-265.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239618888888888,
      "long": 106.25938472222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-266.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239618888888888,
      "long": 106.25938472222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-267.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239618888888888,
      "long": 106.25938472222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-268.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239389166666665,
      "long": 106.25906194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-269.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239389166666665,
      "long": 106.25906194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-270.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239389166666665,
      "long": 106.25906194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-271.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239389166666665,
      "long": 106.25906194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-272.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239389166666665,
      "long": 106.25906194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-273.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239389166666665,
      "long": 106.25906194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-274.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239389166666665,
      "long": 106.25906194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-275.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239389166666665,
      "long": 106.25906194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-276.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239389166666665,
      "long": 106.25906194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-277.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239389166666665,
      "long": 106.25906194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-278.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239389166666665,
      "long": 106.25906194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-279.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239389166666665,
      "long": 106.25906194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-280.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239548333333332,
      "long": 106.25934555555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-281.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239548333333332,
      "long": 106.25934555555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-282.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239548333333332,
      "long": 106.25934555555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-283.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239548333333332,
      "long": 106.25934555555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-284.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239614999999999,
      "long": 106.25936805555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-285.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239614999999999,
      "long": 106.25936805555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-286.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239614999999999,
      "long": 106.25936805555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-287.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239614999999999,
      "long": 106.25936805555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-288.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239614999999999,
      "long": 106.25936805555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-289.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239614999999999,
      "long": 106.25936805555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-290.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239614999999999,
      "long": 106.25936805555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-291.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239614999999999,
      "long": 106.25936805555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-292.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239614999999999,
      "long": 106.25936805555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-293.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239643611111111,
      "long": 106.25948055555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-294.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239643611111111,
      "long": 106.25948055555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-295.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239643611111111,
      "long": 106.25948055555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-296.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239643611111111,
      "long": 106.25948055555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-297.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239643611111111,
      "long": 106.25948055555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-298.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238882222222221,
      "long": 106.2590475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-299.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238882222222221,
      "long": 106.2590475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-300.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238882222222221,
      "long": 106.2590475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-301.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238882222222221,
      "long": 106.2590475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-302.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238882222222221,
      "long": 106.2590475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-303.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238882222222221,
      "long": 106.2590475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-304.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238882222222221,
      "long": 106.2590475
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-305.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23941861111111,
      "long": 106.25919527777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-306.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23941861111111,
      "long": 106.25919527777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-307.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23941861111111,
      "long": 106.25919527777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-308.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23941861111111,
      "long": 106.25919527777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-309.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23941861111111,
      "long": 106.25919527777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-310.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23941861111111,
      "long": 106.25919527777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-311.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23941861111111,
      "long": 106.25919527777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-312.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23941861111111,
      "long": 106.25919527777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-313.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23941861111111,
      "long": 106.25919527777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-314.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23941861111111,
      "long": 106.25919527777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-315.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.23941861111111,
      "long": 106.25919527777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-316.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238805,
      "long": 106.25895277777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-317.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238805,
      "long": 106.25895277777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-318.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239627777777777,
      "long": 106.25939638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-319.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239627777777777,
      "long": 106.25939638888889
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-320.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239559444444444,
      "long": 106.25927055555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-321.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239559444444444,
      "long": 106.25927055555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-322.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239559444444444,
      "long": 106.25927055555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-323.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239559444444444,
      "long": 106.25927055555556
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-324.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239172777777776,
      "long": 106.25906333333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-325.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239172777777776,
      "long": 106.25906333333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-326.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239172777777776,
      "long": 106.25906333333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-327.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239172777777776,
      "long": 106.25906333333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-328.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239172777777776,
      "long": 106.25906333333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-329.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239172777777776,
      "long": 106.25906333333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-330.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239172777777776,
      "long": 106.25906333333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-331.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239172777777776,
      "long": 106.25906333333333
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-332.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.2388125,
      "long": 106.25906
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-333.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238854166666666,
      "long": 106.25905416666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-334.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238854166666666,
      "long": 106.25905416666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-335.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238854166666666,
      "long": 106.25905416666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-336.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238854166666666,
      "long": 106.25905416666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-337.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238854166666666,
      "long": 106.25905416666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-338.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238854166666666,
      "long": 106.25905416666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-339.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238854166666666,
      "long": 106.25905416666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-340.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238807499999998,
      "long": 106.25908861111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-341.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238807499999998,
      "long": 106.25908861111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-342.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238807499999998,
      "long": 106.25908861111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-343.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238807499999998,
      "long": 106.25908861111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-344.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238807499999998,
      "long": 106.25908861111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-345.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238807499999998,
      "long": 106.25908861111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-346.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238807499999998,
      "long": 106.25908861111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-347.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238807499999998,
      "long": 106.25908861111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-348.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238807499999998,
      "long": 106.25908861111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-349.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238807499999998,
      "long": 106.25908861111111
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-350.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238905277777777,
      "long": 106.25907138888888
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-351.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238905277777777,
      "long": 106.25907138888888
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-352.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238905277777777,
      "long": 106.25907138888888
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-353.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238905277777777,
      "long": 106.25907138888888
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-354.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238905277777777,
      "long": 106.25907138888888
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-355.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239596944444443,
      "long": 106.25944777777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-356.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239596944444443,
      "long": 106.25944777777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-357.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239596944444443,
      "long": 106.25944777777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-358.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239596944444443,
      "long": 106.25944777777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-359.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239596944444443,
      "long": 106.25944777777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-360.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239596944444443,
      "long": 106.25944777777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-361.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239596944444443,
      "long": 106.25944777777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-362.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239596944444443,
      "long": 106.25944777777778
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-363.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-364.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-365.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-366.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-367.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-368.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-369.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-370.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-371.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-372.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-373.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-374.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-375.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-376.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-377.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-378.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-379.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-380.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-381.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239635277777777,
      "long": 106.25938416666666
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-382.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-383.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-384.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-385.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-386.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-387.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-388.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-389.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-390.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-391.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-392.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-393.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-394.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-395.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-396.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-397.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-398.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-399.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-420.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238727777777777,
      "long": 106.25855222222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-421.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238727777777777,
      "long": 106.25855222222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-422.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238727777777777,
      "long": 106.25855222222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-423.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238727777777777,
      "long": 106.25855222222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-424.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238727777777777,
      "long": 106.25855222222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-425.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238727777777777,
      "long": 106.25855222222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-426.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238727777777777,
      "long": 106.25855222222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-427.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238727777777777,
      "long": 106.25855222222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-400.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-401.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-402.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-403.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-404.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239476944444444,
      "long": 106.25935194444445
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-405.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239385277777776,
      "long": 106.25919194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-406.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239385277777776,
      "long": 106.25919194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-407.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239385277777776,
      "long": 106.25919194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-408.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239385277777776,
      "long": 106.25919194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-409.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239385277777776,
      "long": 106.25919194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-410.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239385277777776,
      "long": 106.25919194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-411.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239385277777776,
      "long": 106.25919194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-412.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239385277777776,
      "long": 106.25919194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-413.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239385277777776,
      "long": 106.25919194444444
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-414.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239282222222222,
      "long": 106.25938666666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-415.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.239282222222222,
      "long": 106.25938666666667
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-416.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238727777777777,
      "long": 106.25855222222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-417.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238727777777777,
      "long": 106.25855222222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-418.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238727777777777,
      "long": 106.25855222222222
    }
  },
  {
    "url": "camera-storage/047409-250526-143011/photo-419.jpg",
    "type": "photo",
    "pos": {
      "lat": 11.238727777777777,
      "long": 106.25855222222222
    }
  }
]

indexes = []
for item in data:
    url = item["url"]
    # extract index from 'photo-{index}.jpg'
    filename = url.split('/')[-1]
    index = int(filename.split('-')[1].split('.')[0])
    indexes.append(index)

indexes.sort()
print(f"Total elements: {len(indexes)}")
print(f"Min index: {indexes[0]}, Max index: {indexes[-1]}")

missing = []
for i in range(428):
    if i not in indexes:
        missing.append(i)

if missing:
    print(f"Missing indexes: {missing}")
else:
    print("Zero missing indexes! Complete!")
